#!/bin/bash

for BUILD_DIR in joynr-backend sit-jee-app onboard
do
	pushd $BUILD_DIR
	./build_docker_image.sh
	popd
done
