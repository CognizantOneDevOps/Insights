cd ~
mkdir httpd
cd httpd
wget http://archive.apache.org/dist/httpd/httpd-2.4.27.tar.gz
tar -xzvf httpd-2.4.27.tar.gz
cd httpd-2.4.27
mv * ../
cd ..
cd srclib/
mkdir apr
cd apr/
wget http://archive.apache.org/dist/apr/apr-1.6.2.tar.gz
sudo tar -xzvf apr-1.6.2.tar.gz
cd apr-1.6.2
mv * ../
cd ../../
mkdir apr-util
cd apr-util
wget http://archive.apache.org/dist/apr/apr-util-1.6.0.tar.gz
tar -xzvf apr-util-1.6.0.tar.gz
cd apr-util-1.6.0
mv * ../
cd ~
mkdir pcre
cd pcre
wget https://ftp.pcre.org/pub/pcre/pcre-8.00.tar.gz
tar -xzvf pcre-8.00.tar.gz
cd pcre-8.00
mv * ../
sudo yum groupinstall "Development tools" -y
cd ..
./configure --prefix=/usr/local/pcre
make
sudo make install
cd ~
cd httpd
./configure --with-pcre=/usr/local/pcre
sudo yum install expat-devel -y
make clean
make
sudo make install
sudo /usr/local/apache2/bin/apachectl -k start
curl localhost