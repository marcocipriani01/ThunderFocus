#!/bin/bash
echo "Checking required packages..."
if [[ -z "$(which java)" ]]; then
    echo "Java not found!"
    exit 3
fi
if [[ -z "$(which socat)" ]]; then
    echo "socat not found!"
    read -p "Do you want to install it (y/n)? " ri
    if [[ "$ri" == "y" ]]; then
        echo "Installing..."
        sudo apt-get install -y socat
        echo "Done."

    else
        exit 4
    fi
fi
if [[ -z "$(which indiserver)" ]]; then
    echo "INDI not found!"
    exit 5
fi
if [[ -z "$(which avrdude)" ]]; then
    echo "avrdude not found!"
    read -p "Do you want to install it (y/n)? " ri
    if [[ "$ri" == "y" ]]; then
        echo "Installing..."
        sudo apt-get install -y avrdude
        echo "Done."

    else
        exit 6
    fi
fi

installDir="$(dirname "$0")/out/artifacts/OpenFocuser_Manager_jar/OpenFocuser-Manager.jar"
if [[ ! -f "$installDir" ]]; then
    echo "OpenFocuser-Manager.jar not found! Please build the module first!"
    exit 7
fi

java -jar "$installDir" "-v" "$@"