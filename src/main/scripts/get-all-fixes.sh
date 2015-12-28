#!/bin/sh
#
# Linux Shell Script to recover different versions of the same file from a git repository
#
# Written by Arnaud Hubaux <ahubaux@gmail.com>
#
# USAGE
# sh get-all-fixes
echo "VIRTEX-4" ;
./compute-fixes.sh /home/0-virtex-4/core/ecos/ecos-patched/ecos/packages/ /home/0-virtex-4/config/ ;
./compute-fixes.sh /home/0-virtex-4/core/ecos/ecos-patched/ecos/packages/ /home/0-virtex-4-default/ ;
echo "XILINX" ;
./compute-fixes.sh /home/0-virtex-4/core/ecos/ecos-patched/ecos/packages/ /home/2-xilinx_ml605_light/config/ ;
./compute-fixes.sh /home/0-virtex-4/core/ecos/ecos-patched/ecos/packages/ /home/2-xilinx_ml605_light-default/ ;
echo "GPS" ;
./compute-fixes.sh /opt/ecos/ecos-3.0/packages/ /home/2860-gps4020/config/ ;
./compute-fixes.sh /opt/ecos/ecos-3.0/packages/ /home/2860-gps4020-default/ ;
echo "EA";
./compute-fixes.sh /opt/ecos/ecos-3.0/packages/ /home/3-ea2468/config/ ;
./compute-fixes.sh /opt/ecos/ecos-3.0/packages/ /home/3-ea2468-default/ ;
echo "AKI";
./compute-fixes.sh /opt/ecos/ecos-3.0/packages/ /home/52-aki3068net/config/ ;
./compute-fixes.sh /opt/ecos/ecos-3.0/packages/ /home/52-aki3068net-default/ ;