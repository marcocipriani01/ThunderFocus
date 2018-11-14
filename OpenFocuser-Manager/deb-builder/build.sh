#!/bin/bash
function build() {
    cd $1
    chown -R root:root OpenFocuser-Manager/
    echo "Invoking dpkg-deb..."
    dpkg-deb --build OpenFocuser-Manager
    chown -R ${2}:${2} OpenFocuser-Manager/
    chown -R ${2}:${2} OpenFocuser-Manager.deb
    DISPLAY=$3 zenity --question --title="OpenFocuser-Manager" --text="Package created. Install it now?" --width=250 2>/dev/null
    if [[ "$?" == "0" ]]; then
        dpkg -i ./OpenFocuser-Manager.deb
    fi
}

cd "$(dirname "$0")"
echo "Cleaning..."
rm ./OpenFocuser-Manager.deb 2> /dev/null
echo "Copying files..."
cp ../out/artifacts/OpenFocuser_Manager_jar/OpenFocuser-Manager.jar OpenFocuser-Manager/usr/share/OpenFocuser-Manager/OpenFocuser-Manager.jar
cp ../logo.png OpenFocuser-Manager/usr/share/OpenFocuser-Manager/logo.png
echo "Setting permissions..."
chmod +x OpenFocuser-Manager/usr/share/applications/OpenFocuser-Manager.desktop
chmod +x OpenFocuser-Manager/usr/share/applications/OpenFocuser-Manager-server.desktop
chmod +x OpenFocuser-Manager/usr/bin/openfocuser
xhost local:root
if [[ -n "$(which gksudo)" ]]; then
    gksudo bash -c "$(declare -f build); build \"$(pwd)\" \"${USER}\" \"${DISPLAY}\""

elif [[ -n "$(which kdesudo)" ]]; then
    kdesudo bash -c "$(declare -f build); build \"$(pwd)\" \"${USER}\" \"${DISPLAY}\""

elif [[ -n "$(which pkexec)" ]]; then
    pkexec bash -c "$(declare -f build); build \"$(pwd)\" \"${USER}\" \"${DISPLAY}\""

else
    sudo chown -R root:root OpenFocuser-Manager/
    echo "Invoking dpkg-deb..."
    dpkg-deb --build OpenFocuser-Manager
    sudo chown -R ${USER}:${USER} OpenFocuser-Manager/
    sudo chown -R ${USER}:${USER} OpenFocuser-Manager.deb
    read -p "Do you want to install the deb file now (y/n)? " re
    if [[ "$re" == "y" ]]; then
        dpkg -i ./OpenFocuser-Manager.deb
    fi
fi