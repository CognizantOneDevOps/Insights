# install postgresql
echo "#################### Installing Postgres with configs , Databases and Roles ####################"
sudo yum install http://platform.cogdevops.com/InSightsV1.0/postgres/pgdg-redhat95-9.5-2.noarch.rpm -y
sudo yum install postgresql95-server postgresql95-contrib -y
sudo /usr/pgsql-9.5/bin/postgresql95-setup initdb
sudo systemctl enable postgresql-9.5.service
sudo chkconfig postgresql-9.5 on
sudo wget http://platform.cogdevops.com/InSightsV1.0/postgres/pg_hba.conf
sudo cp pg_hba.conf /var/lib/pgsql/9.5/data/pg_hba.conf
sudo systemctl start  postgresql-9.5.service
sudo useradd grafana
sudo usermod --password C0gnizant@1 grafana
sudo wget http://platform.cogdevops.com/InSightsV1.0/postgres/dbscript.sql
sudo chmod +x dbscript.sql
psql -U postgres -f dbscript.sql