ALTER TABLE widget_type
    ADD COLUMN IF NOT EXISTS image varchar (1000000),
    ADD COLUMN IF NOT EXISTS description varchar (255);

ALTER TABLE widgets_bundle
    ADD COLUMN IF NOT EXISTS image varchar (1000000),
    ADD COLUMN IF NOT EXISTS description varchar (255);
