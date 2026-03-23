sudo apt update
sudo apt install -y postgresql postgresql-contrib
sudo systemctl status postgresql
sudo -u postgres psql -c "ALTER USER postgres WITH PASSWORD 'postgres'"
sudo chmod -R 777 /etc/postgresql/16/main/
sudo chmod -R 777 /etc/postgresql/16/main/
sudo echo -e "\nlisten_addresses='*'" >> "/etc/postgresql/16/main/postgresql.conf"
sudo invoke-rc.d postgresql restart
sudo echo -e "\nhost all all all md5" >> "/etc/postgresql/16/main/pg_hba.conf"
sudo invoke-rc.d postgresql reload
sudo -u postgres psql -c "CREATE DATABASE minitwit"
