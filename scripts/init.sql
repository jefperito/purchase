
CREATE TABLE public.purchase (
     id uuid NOT NULL,
     idempotency_key varchar(255) NOT NULL,
     description varchar(255) NOT NULL,
     transaction_date timestamptz NOT NULL,
     amount numeric(38, 2) NOT NULL,
     CONSTRAINT purchase_pk PRIMARY KEY (id),
     CONSTRAINT purchase_idempotency_unique UNIQUE (idempotency_key)
);

CREATE INDEX purchase_idk_hash ON public.purchase USING hash (idempotency_key);
