#! /usr/bin/env python

class Tables:
	
	tables = (
	 ('chip',('id Integer primary key' , 'name Varchar(30) unique'))
	,('chip_group',('id Integer primary key', 'address Varchar(20)', 'status Integer', 'comment Varchar(30)', 'x Integer', 'y Integer', 'group_id Integer references chip_group'))
	,('temp',('chip_addr Varchar(20) references chip', 'value Float', 'time Integer'))
	,('light',('chip_addr Varchar(20) references chip', 'value Float', 'time Integer'))
	,('humidity',('chip_addr Varchar(20) references chip', 'value Float', 'time Integer'))
	,('pressure',('chip_addr Varchar(20) references chip', 'value Float', 'time Integer'))
	)
