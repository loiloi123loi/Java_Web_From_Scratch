-- Create Databases
CREATE DATABASE IF NOT EXISTS smart_class_dev;
CREATE DATABASE IF NOT EXISTS smart_class_pro;
CREATE DATABASE IF NOT EXISTS smart_class_test_1;
CREATE DATABASE IF NOT EXISTS smart_class_test_2;

-- Create Dev User and Grant Permissions
CREATE USER IF NOT EXISTS 'dev_user'@'%' IDENTIFIED BY 'dev_password_123';
GRANT ALL PRIVILEGES ON smart_class_dev.* TO 'dev_user'@'%';
GRANT ALL PRIVILEGES ON smart_class_test_1.* TO 'dev_user'@'%';
GRANT ALL PRIVILEGES ON smart_class_test_2.* TO 'dev_user'@'%';

-- Create Pro User and Grant Permissions
CREATE USER IF NOT EXISTS 'pro_user'@'%' IDENTIFIED BY 'pro_password_123';
GRANT ALL PRIVILEGES ON smart_class_pro.* TO 'pro_user'@'%';

FLUSH PRIVILEGES;
