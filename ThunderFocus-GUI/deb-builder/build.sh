#!/bin/bash
cd "$(dirname "$0")"
echo "Cleaning..."
rm ./ThunderFocus.deb 2> /dev/null
echo "Copying files..."
cp ../out/artifacts/ThunderFocus_jar/ThunderFocus.jar ThunderFocus/usr/share/ThunderFocus/ThunderFocus.jar
echo "Setting permissions..."
chmod +x ThunderFocus/usr/share/applications/ThunderFocus.desktop
chmod +x ThunderFocus/usr/bin/thunderfocus
sudo chown -R root:root ThunderFocus/
echo "Invoking dpkg-deb..."
dpkg-deb --build ThunderFocus
sudo chown -R ${USER}:${USER} ThunderFocus/
sudo chown -R ${USER}:${USER} ThunderFocus.deb
read -p "Do you want to install the deb file now (y/n)? " re
if [[ "$re" == "y" ]]; then
    dpkg -i ./ThunderFocus.deb
fi
mv ThunderFocus.deb ../../Installers
