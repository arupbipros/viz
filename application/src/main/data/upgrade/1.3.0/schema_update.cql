DROP MATERIALIZED VIEW IF EXISTS vizzionnaire.device_by_tenant_and_name;
DROP MATERIALIZED VIEW IF EXISTS vizzionnaire.device_by_tenant_and_search_text;
DROP MATERIALIZED VIEW IF EXISTS vizzionnaire.device_by_tenant_by_type_and_search_text;
DROP MATERIALIZED VIEW IF EXISTS vizzionnaire.device_by_customer_and_search_text;
DROP MATERIALIZED VIEW IF EXISTS vizzionnaire.device_by_customer_by_type_and_search_text;
DROP MATERIALIZED VIEW IF EXISTS vizzionnaire.device_types_by_tenant;

DROP TABLE IF EXISTS vizzionnaire.device;

CREATE TABLE IF NOT EXISTS vizzionnaire.device (
    id timeuuid,
    tenant_id timeuuid,
    customer_id timeuuid,
    name text,
    type text,
    search_text text,
    additional_info text,
    PRIMARY KEY (id, tenant_id, customer_id, type)
);

CREATE MATERIALIZED VIEW IF NOT EXISTS vizzionnaire.device_by_tenant_and_name AS
    SELECT *
    from vizzionnaire.device
    WHERE tenant_id IS NOT NULL AND customer_id IS NOT NULL AND type IS NOT NULL AND name IS NOT NULL AND id IS NOT NULL
    PRIMARY KEY ( tenant_id, name, id, customer_id, type)
    WITH CLUSTERING ORDER BY ( name ASC, id DESC, customer_id DESC);

CREATE MATERIALIZED VIEW IF NOT EXISTS vizzionnaire.device_by_tenant_and_search_text AS
    SELECT *
    from vizzionnaire.device
    WHERE tenant_id IS NOT NULL AND customer_id IS NOT NULL AND type IS NOT NULL AND search_text IS NOT NULL AND id IS NOT NULL
    PRIMARY KEY ( tenant_id, search_text, id, customer_id, type)
    WITH CLUSTERING ORDER BY ( search_text ASC, id DESC, customer_id DESC);

CREATE MATERIALIZED VIEW IF NOT EXISTS vizzionnaire.device_by_tenant_by_type_and_search_text AS
    SELECT *
    from vizzionnaire.device
    WHERE tenant_id IS NOT NULL AND customer_id IS NOT NULL AND type IS NOT NULL AND search_text IS NOT NULL AND id IS NOT NULL
    PRIMARY KEY ( tenant_id, type, search_text, id, customer_id)
    WITH CLUSTERING ORDER BY ( type ASC, search_text ASC, id DESC, customer_id DESC);

CREATE MATERIALIZED VIEW IF NOT EXISTS vizzionnaire.device_by_customer_and_search_text AS
    SELECT *
    from vizzionnaire.device
    WHERE tenant_id IS NOT NULL AND customer_id IS NOT NULL AND type IS NOT NULL AND search_text IS NOT NULL AND id IS NOT NULL
    PRIMARY KEY ( customer_id, tenant_id, search_text, id, type )
    WITH CLUSTERING ORDER BY ( tenant_id DESC, search_text ASC, id DESC );

CREATE MATERIALIZED VIEW IF NOT EXISTS vizzionnaire.device_by_customer_by_type_and_search_text AS
    SELECT *
    from vizzionnaire.device
    WHERE tenant_id IS NOT NULL AND customer_id IS NOT NULL AND type IS NOT NULL AND search_text IS NOT NULL AND id IS NOT NULL
    PRIMARY KEY ( customer_id, tenant_id, type, search_text, id )
    WITH CLUSTERING ORDER BY ( tenant_id DESC, type ASC, search_text ASC, id DESC );

DROP MATERIALIZED VIEW IF EXISTS vizzionnaire.asset_by_tenant_and_name;
DROP MATERIALIZED VIEW IF EXISTS vizzionnaire.asset_by_tenant_and_search_text;
DROP MATERIALIZED VIEW IF EXISTS vizzionnaire.asset_by_tenant_by_type_and_search_text;
DROP MATERIALIZED VIEW IF EXISTS vizzionnaire.asset_by_customer_and_search_text;
DROP MATERIALIZED VIEW IF EXISTS vizzionnaire.asset_by_customer_by_type_and_search_text;
DROP MATERIALIZED VIEW IF EXISTS vizzionnaire.asset_types_by_tenant;

DROP TABLE IF EXISTS vizzionnaire.asset;

CREATE TABLE IF NOT EXISTS vizzionnaire.asset (
    id timeuuid,
    tenant_id timeuuid,
    customer_id timeuuid,
    name text,
    type text,
    search_text text,
    additional_info text,
    PRIMARY KEY (id, tenant_id, customer_id, type)
);

CREATE MATERIALIZED VIEW IF NOT EXISTS vizzionnaire.asset_by_tenant_and_name AS
    SELECT *
    from vizzionnaire.asset
    WHERE tenant_id IS NOT NULL AND customer_id IS NOT NULL AND type IS NOT NULL AND name IS NOT NULL AND id IS NOT NULL
    PRIMARY KEY ( tenant_id, name, id, customer_id, type)
    WITH CLUSTERING ORDER BY ( name ASC, id DESC, customer_id DESC);

CREATE MATERIALIZED VIEW IF NOT EXISTS vizzionnaire.asset_by_tenant_and_search_text AS
    SELECT *
    from vizzionnaire.asset
    WHERE tenant_id IS NOT NULL AND customer_id IS NOT NULL AND type IS NOT NULL AND search_text IS NOT NULL AND id IS NOT NULL
    PRIMARY KEY ( tenant_id, search_text, id, customer_id, type)
    WITH CLUSTERING ORDER BY ( search_text ASC, id DESC, customer_id DESC);

CREATE MATERIALIZED VIEW IF NOT EXISTS vizzionnaire.asset_by_tenant_by_type_and_search_text AS
    SELECT *
    from vizzionnaire.asset
    WHERE tenant_id IS NOT NULL AND customer_id IS NOT NULL AND type IS NOT NULL AND search_text IS NOT NULL AND id IS NOT NULL
    PRIMARY KEY ( tenant_id, type, search_text, id, customer_id)
    WITH CLUSTERING ORDER BY ( type ASC, search_text ASC, id DESC, customer_id DESC);

CREATE MATERIALIZED VIEW IF NOT EXISTS vizzionnaire.asset_by_customer_and_search_text AS
    SELECT *
    from vizzionnaire.asset
    WHERE tenant_id IS NOT NULL AND customer_id IS NOT NULL AND type IS NOT NULL AND search_text IS NOT NULL AND id IS NOT NULL
    PRIMARY KEY ( customer_id, tenant_id, search_text, id, type )
    WITH CLUSTERING ORDER BY ( tenant_id DESC, search_text ASC, id DESC );

CREATE MATERIALIZED VIEW IF NOT EXISTS vizzionnaire.asset_by_customer_by_type_and_search_text AS
    SELECT *
    from vizzionnaire.asset
    WHERE tenant_id IS NOT NULL AND customer_id IS NOT NULL AND type IS NOT NULL AND search_text IS NOT NULL AND id IS NOT NULL
    PRIMARY KEY ( customer_id, tenant_id, type, search_text, id )
    WITH CLUSTERING ORDER BY ( tenant_id DESC, type ASC, search_text ASC, id DESC );

CREATE TABLE IF NOT EXISTS vizzionnaire.entity_subtype (
    tenant_id timeuuid,
    entity_type text, // (DEVICE, ASSET)
    type text,
    PRIMARY KEY (tenant_id, entity_type, type)
);

CREATE TABLE IF NOT EXISTS vizzionnaire.alarm (
    id timeuuid,
    tenant_id timeuuid,
    type text,
    originator_id timeuuid,
    originator_type text,
    severity text,
    status text,
    start_ts bigint,
    end_ts bigint,
    ack_ts bigint,
    clear_ts bigint,
    details text,
    propagate boolean,
    PRIMARY KEY ((tenant_id, originator_id, originator_type), type, id)
) WITH CLUSTERING ORDER BY ( type ASC, id DESC);

CREATE MATERIALIZED VIEW IF NOT EXISTS vizzionnaire.alarm_by_id AS
    SELECT *
    from vizzionnaire.alarm
    WHERE tenant_id IS NOT NULL AND originator_id IS NOT NULL AND originator_type IS NOT NULL AND type IS NOT NULL
    AND type IS NOT NULL AND id IS NOT NULL
    PRIMARY KEY (id, tenant_id, originator_id, originator_type, type)
    WITH CLUSTERING ORDER BY ( tenant_id ASC, originator_id ASC, originator_type ASC, type ASC);

DROP MATERIALIZED VIEW IF EXISTS vizzionnaire.relation_by_type_and_child_type;
DROP MATERIALIZED VIEW IF EXISTS vizzionnaire.reverse_relation;

DROP TABLE IF EXISTS vizzionnaire.relation;

CREATE TABLE IF NOT EXISTS vizzionnaire.relation (
    from_id timeuuid,
    from_type text,
    to_id timeuuid,
    to_type text,
    relation_type_group text,
    relation_type text,
    additional_info text,
    PRIMARY KEY ((from_id, from_type), relation_type_group, relation_type, to_id, to_type)
) WITH CLUSTERING ORDER BY ( relation_type_group ASC, relation_type ASC, to_id ASC, to_type ASC);

CREATE MATERIALIZED VIEW IF NOT EXISTS vizzionnaire.relation_by_type_and_child_type AS
    SELECT *
    from vizzionnaire.relation
    WHERE from_id IS NOT NULL AND from_type IS NOT NULL AND relation_type_group IS NOT NULL AND relation_type IS NOT NULL AND to_id IS NOT NULL AND to_type IS NOT NULL
    PRIMARY KEY ((from_id, from_type), relation_type_group, relation_type, to_type, to_id)
    WITH CLUSTERING ORDER BY ( relation_type_group ASC, relation_type ASC, to_type ASC, to_id DESC);

CREATE MATERIALIZED VIEW IF NOT EXISTS vizzionnaire.reverse_relation AS
    SELECT *
    from vizzionnaire.relation
    WHERE from_id IS NOT NULL AND from_type IS NOT NULL AND relation_type_group IS NOT NULL AND relation_type IS NOT NULL AND to_id IS NOT NULL AND to_type IS NOT NULL
    PRIMARY KEY ((to_id, to_type), relation_type_group, relation_type, from_id, from_type)
    WITH CLUSTERING ORDER BY ( relation_type_group ASC, relation_type ASC, from_id ASC, from_type ASC);
