CREATE TABLE IF NOT EXISTS vizzionnaire.entity_views (
    id timeuuid,
    entity_id timeuuid,
    entity_type text,
    tenant_id timeuuid,
    customer_id timeuuid,
    name text,
    keys text,
    start_ts bigint,
    end_ts bigint,
    search_text text,
    additional_info text,
    PRIMARY KEY (id, entity_id, tenant_id, customer_id)
);

CREATE MATERIALIZED VIEW IF NOT EXISTS vizzionnaire.entity_view_by_tenant_and_name AS
    SELECT *
    from vizzionnaire.entity_views
    WHERE tenant_id IS NOT NULL
        AND entity_id IS NOT NULL
        AND customer_id IS NOT NULL
        AND name IS NOT NULL
        AND id IS NOT NULL
    PRIMARY KEY (tenant_id, name, id, customer_id, entity_id)
    WITH CLUSTERING ORDER BY (name ASC, id DESC, customer_id DESC);

CREATE MATERIALIZED VIEW IF NOT EXISTS vizzionnaire.entity_view_by_tenant_and_search_text AS
    SELECT *
    from vizzionnaire.entity_views
    WHERE tenant_id IS NOT NULL
      AND entity_id IS NOT NULL
      AND customer_id IS NOT NULL
      AND search_text IS NOT NULL
      AND id IS NOT NULL
    PRIMARY KEY (tenant_id, search_text, id, customer_id, entity_id)
    WITH CLUSTERING ORDER BY (search_text ASC, id DESC, customer_id DESC);

CREATE MATERIALIZED VIEW IF NOT EXISTS vizzionnaire.entity_view_by_tenant_and_customer AS
    SELECT *
    from vizzionnaire.entity_views
    WHERE tenant_id IS NOT NULL
      AND customer_id IS NOT NULL
      AND entity_id IS NOT NULL
      AND search_text IS NOT NULL
      AND id IS NOT NULL
    PRIMARY KEY (tenant_id, customer_id, search_text, id, entity_id)
    WITH CLUSTERING ORDER BY (customer_id DESC, search_text ASC, id DESC);

CREATE MATERIALIZED VIEW IF NOT EXISTS vizzionnaire.entity_view_by_tenant_and_entity_id AS
    SELECT *
    from vizzionnaire.entity_views
    WHERE tenant_id IS NOT NULL
      AND customer_id IS NOT NULL
      AND entity_id IS NOT NULL
      AND search_text IS NOT NULL
      AND id IS NOT NULL
    PRIMARY KEY (tenant_id, entity_id, customer_id, search_text, id)
    WITH CLUSTERING ORDER BY (entity_id DESC, customer_id DESC, search_text ASC, id DESC);