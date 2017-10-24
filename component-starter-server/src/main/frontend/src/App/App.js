/**
 *  Copyright (C) 2006-2017 Talend Inc. - www.talend.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
import React from 'react';
import { BrowserRouter as Router, Route } from 'react-router-dom';
import IconsProvider from '@talend/react-components/lib/IconsProvider';
import HeaderBar from '@talend/react-components/lib/HeaderBar';
import Generator from '../Generator';

import theme from './App.scss';
import './favicon.ico';

export default function App() {
	return (
		<Router>
			<div className={theme.App}>
				<IconsProvider/>

				<div className={theme.header}>
					<HeaderBar logo={{ isFull: true }} brand={{ name: 'Component Kit Starter' }} />
				</div>

				<div className={theme.content}>
					<Route exact path="/" component={Generator}/>
				</div>
			</div>
		</Router>
	);
}