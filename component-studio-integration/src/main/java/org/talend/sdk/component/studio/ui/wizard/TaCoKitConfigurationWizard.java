/**
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.talend.sdk.component.studio.ui.wizard;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ui.IWorkbench;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.exception.ExceptionMessageDialog;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.metadata.managment.ui.wizard.CheckLastVersionRepositoryWizard;
import org.talend.metadata.managment.ui.wizard.metadata.connection.Step0WizardPage;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.RepositoryNodeUtilities;
import org.talend.sdk.component.server.front.model.ConfigTypeNode;
import org.talend.sdk.component.studio.i18n.Messages;
import org.talend.sdk.component.studio.metadata.node.ITaCoKitRepositoryNode;
import org.talend.sdk.component.studio.ui.wizard.page.TaCoKitConfigurationWizardPage;
import org.talend.sdk.component.studio.util.TaCoKitConst;

import lombok.Getter;

/**
 * Base class for Repository Metadata wizards
 */
@Getter(lombok.AccessLevel.PROTECTED)
public abstract class TaCoKitConfigurationWizard extends CheckLastVersionRepositoryWizard {

    private final TaCoKitConfigurationRuntimeData runtimeData;

    private Step0WizardPage wizardPropertiesPage;

    private TaCoKitConfigurationWizardPage configurationPage;

    public TaCoKitConfigurationWizard(final IWorkbench workbench, final TaCoKitConfigurationRuntimeData runtimeData) {
        super(workbench, runtimeData.isCreation(), runtimeData.isReadonly());
        this.runtimeData = runtimeData;
        this.existingNames = runtimeData.getExistingNames();
        creation = runtimeData.isCreation();
        connectionItem = runtimeData.getConnectionItem();
        this.runtimeData.setReadonly(runtimeData.isReadonly() || !isRepositoryObjectEditable());
        init();
    }

    /**
     * Part of constructor
     */
    private void init() {
        setPathToSave();
        lockNode();
        setWindowTitle();
        setHelpAvailable(false);
        // setRepositoryLocation(wizard, location, connectionItem.getProperty().getId());
    }

    /**
     * Part of constructor
     * Sets {@code pathToSave} if repository node has one of following types:
     * <ul>
     * <li>SIMPLE_FOLDER</li>
     * <li>SYSTEM_FOLDER</li>
     * <li>REPOSITORY_ELEMENT</li>
     * </ul>
     */
    private void setPathToSave() {
        ITaCoKitRepositoryNode repositoryNode = runtimeData.getTaCoKitRepositoryNode();
        ENodeType nodeType = repositoryNode.getType();
        switch (nodeType) {
        case SIMPLE_FOLDER:
        case REPOSITORY_ELEMENT:
            pathToSave = RepositoryNodeUtilities.getPath(repositoryNode);
            break;
        case SYSTEM_FOLDER:
            pathToSave = new Path(""); //$NON-NLS-1$
            break;
        default:
            // nothing todo
        }
    }

    /**
     * Part of constructor
     * Initializes lock strategy if repository node type is REPOSITORY_ELEMENT
     * If needed, locks the repository node
     */
    private void lockNode() {
        ITaCoKitRepositoryNode repositoryNode = runtimeData.getTaCoKitRepositoryNode();
        ENodeType nodeType = repositoryNode.getType();
        if (ENodeType.REPOSITORY_ELEMENT.equals(nodeType)) {
            initLockStrategy();
        }
    }

    /**
     * Part of constructor, sets window title
     */
    protected abstract void setWindowTitle();

    @Override
    public void addPages() {
        wizardPropertiesPage = new Step0WizardPage(runtimeData.getConnectionItem().getProperty(), pathToSave,
                TaCoKitConst.METADATA_TACOKIT, runtimeData.isReadonly(), creation);
        ConfigTypeNode configTypeNode = runtimeData.getConfigTypeNode();
        wizardPropertiesPage.setTitle(Messages.getString("TaCoKitConfiguration.wizard.title", //$NON-NLS-1$
                configTypeNode.getConfigurationType(), configTypeNode.getDisplayName()));
        wizardPropertiesPage.setDescription(""); //$NON-NLS-1$
        addPage(wizardPropertiesPage);

        configurationPage = new TaCoKitConfigurationWizardPage(runtimeData);
        addPage(configurationPage);

    }

    @Override
    public boolean performFinish() {
        if (configurationPage.isPageComplete()) {
            try {
                IWorkspace workspace = ResourcesPlugin.getWorkspace();
                IWorkspaceRunnable operation = createFinishOperation();
                ISchedulingRule schedulingRule = workspace.getRoot();
                workspace.run(operation, schedulingRule, IWorkspace.AVOID_UPDATE, new NullProgressMonitor());
                return true;
            } catch (Exception e) {
                ExceptionHandler.process(e);
                String message = e.getLocalizedMessage();
                if (StringUtils.isEmpty(message)) {
                    message = Messages.getString("TaCoKitConfiguration.wizard.exception.message.default"); //$NON-NLS-1$
                }
                ExceptionMessageDialog.openError(getShell(),
                        Messages.getString("TaCoKitConfiguration.wizard.exception.title"), //$NON-NLS-1$
                        message, e);
            }
        }
        return false;
    }

    /**
     * Creates operation, which is performed, when Finish button is pushed.
     * Creates different operations depending on whether it is Create or Edit wizard
     * 
     * @return operation to perform on finish
     */
    protected abstract IWorkspaceRunnable createFinishOperation();

    @Override
    public boolean canFinish() {
        if (getRuntimeData().isReadonly()) {
            return false;
        }
        IWizardPage currentPage = this.getContainer().getCurrentPage();
        if (currentPage instanceof Step0WizardPage) {
            return false;
        }
        if (currentPage.isPageComplete()) {
            return true;
        }
        return false;
    }
    
    @Override
    public ConnectionItem getConnectionItem() {
        return connectionItem;
    }

}
