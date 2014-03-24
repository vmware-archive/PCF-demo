CREATE SCHEMA pcf_demo;

create table pcf_demo.ORDERS (

    ORDER_ID int,
    CUSTOMER_ID int,
    ORDER_AMOUNT int,
    STORE_ID varchar(30),
	STATE_ID varchar(30),
	CITY_ID varchar(30),
	PRIMARY KEY(ORDER_ID)
);


INSERT INTO pcf_demo.ORDERS values (1,1,100,"15","20","20");
