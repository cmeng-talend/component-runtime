#! /bin/bash

if [ "$TRAVIS_PULL_REQUEST" = "true" ]; then
    exit 0
fi

mkdir -p ~/.gpg/
openssl aes-256-cbc -K $encrypted_03e441b90173_key -iv $encrypted_03e441b90173_iv -in .travis/gpg/gpg.tar.enc -out ~/.gpg/gpg.tar -d
tar xvf ~/.gpg/gpg.tar -C ~/.gpg/
for i in priv pub; do
    cat ~/.gpg/travis.$i | gpg --dearmor > ~/.gpg/travis.$i.bin
done