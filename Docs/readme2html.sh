#!/bin/bash
if [[ -z "$(which pandoc)" ]]; then
    sudo apt install -y pandoc
fi
cd $(dirname "$0")/..

echo "============ pandoc README ============"
pandoc README.md -f markdown -t html -s --css=readme-style.css -o Readme.html

echo "============ pandoc README-it ============"
pandoc README-it.md -f markdown -t html -s --css=readme-style.css -o Readme-it.html

echo "============ md_toc README ============"
md_toc README.md github

echo "============ md_toc README-it ============"
md_toc README-it.md github
