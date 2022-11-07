/** SYSTEM **/

/** System admin **/
INSERT INTO tb_user ( id, created_time, tenant_id, customer_id, email, search_text, authority )
VALUES ( '5a797660-4612-11e7-a919-92ebcb67fe33', 1592576748000, '13814000-1dd2-11b2-8080-808080808080', '13814000-1dd2-11b2-8080-808080808080', 'sysadmin@thingsboard.org',
         'sysadmin@thingsboard.org', 'SYS_ADMIN' );

INSERT INTO user_credentials ( id, created_time, user_id, enabled, password )
VALUES ( '61441950-4612-11e7-a919-92ebcb67fe33', 1592576748000, '5a797660-4612-11e7-a919-92ebcb67fe33', true,
         '$2a$10$5JTB8/hxWc9WAy62nCGSxeefl3KWmipA9nFpVdDa0/xfIseeBB4Bu' );

/** System settings **/
INSERT INTO admin_settings ( id, created_time, tenant_id, key, json_value )
VALUES ( '6a2266e4-4612-11e7-a919-92ebcb67fe33', 1592576748000, '13814000-1dd2-11b2-8080-808080808080', 'general', '{
	"baseUrl": "http://localhost:8080"
}' );

INSERT INTO admin_settings ( id, created_time, tenant_id, key, json_value )
VALUES ( '6eaaefa6-4612-11e7-a919-92ebcb67fe33', 1592576748000, '13814000-1dd2-11b2-8080-808080808080', 'mail', '{
	"mailFrom": "Thingsboard <sysadmin@localhost.localdomain>",
	"smtpProtocol": "smtp",
	"smtpHost": "localhost",
	"smtpPort": "25",
	"timeout": "10000",
	"enableTls": false,
	"tlsVersion": "TLSv1.2",
	"username": "",
	"password": ""
}' );

INSERT INTO queue ( id, created_time, tenant_id, name, topic, poll_interval, partitions, consumer_per_partition, pack_processing_timeout, submit_strategy, processing_strategy )
VALUES ( '6eaaefa6-4612-11e7-a919-92ebcb67fe33', 1592576748000 ,'13814000-1dd2-11b2-8080-808080808080', 'Main' ,'tb_rule_engine.main', 25, 10, true, 2000,
        '{"type": "BURST", "batchSize": 1000}',
        '{"type": "SKIP_ALL_FAILURES", "retries": 3, "failurePercentage": 0.0, "pauseBetweenRetries": 3, "maxPauseBetweenRetries": 3}'
);
