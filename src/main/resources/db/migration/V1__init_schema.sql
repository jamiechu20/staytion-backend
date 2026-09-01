CREATE TABLE cities (
    id SERIAL PRIMARY KEY,
    cname varchar(100) NOT NULL,
    img_url text NOT NULL
);

CREATE TABLE owners (
    id SERIAL PRIMARY KEY,
    first_name varchar(100) NOT NULL,
    last_name varchar(100) NOT NULL,
    email varchar(100) NOT NULL,
    password varchar(1000) NOT NULL,
    tel varchar(100) NOT NULL
);

CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    first_name varchar(100) NOT NULL,
    last_name varchar(100) NOT NULL,
    email varchar(100) NOT NULL,
    password varchar(1000) NOT NULL,
    tel varchar(100) NOT NULL,
    created_at timestamp NOT NULL,
    UNIQUE (email)
);

CREATE TABLE amenities (
    id SERIAL PRIMARY KEY,
    aname varchar(100) NOT NULL,
    UNIQUE (aname)
);

CREATE TABLE districts (
    id SERIAL PRIMARY KEY,
    dname varchar(100) NOT NULL,
    city_id integer NOT NULL,
    CONSTRAINT fk_districts_city FOREIGN KEY (city_id) REFERENCES cities (id) ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE INDEX idx_districts_city_id ON districts (city_id);

CREATE TABLE hotels (
    id SERIAL PRIMARY KEY,
    hname varchar(100) NOT NULL,
    address varchar(1000) NOT NULL,
    tel varchar(100) NOT NULL,
    description text,
    district_id integer NOT NULL,
    latitude double precision,
    longitude double precision,
    owner_id integer,
    CONSTRAINT fk_hotels_district FOREIGN KEY (district_id) REFERENCES districts (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_hotels_owner FOREIGN KEY (owner_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE INDEX idx_hotels_district_id ON hotels (district_id);
CREATE INDEX idx_hotels_owner_id ON hotels (owner_id);

CREATE TABLE hotel_amenities (
    hotel_id integer NOT NULL,
    amenities_id integer NOT NULL,
    PRIMARY KEY (hotel_id, amenities_id)
);

CREATE TABLE room_types (
    id SERIAL PRIMARY KEY,
    hotel_id integer NOT NULL,
    rname varchar(100) NOT NULL,
    price decimal(10,0) NOT NULL,
    description text,
    size integer NOT NULL,
    view varchar(100),
    img_url text NOT NULL,
    is_canceled boolean NOT NULL DEFAULT false,
    quantity integer NOT NULL,
    bed_count integer NOT NULL,
    bed_type varchar(100) NOT NULL,
    capacity integer NOT NULL,
    CONSTRAINT fk_room_types_hotel FOREIGN KEY (hotel_id) REFERENCES hotels (id) ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE INDEX idx_room_types_hotel_id ON room_types (hotel_id);

CREATE TABLE room_amenities (
    room_type_id integer NOT NULL,
    amenity_id integer NOT NULL,
    PRIMARY KEY (room_type_id, amenity_id),
    CONSTRAINT fk_room_amenities_room_type FOREIGN KEY (room_type_id) REFERENCES room_types (id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE room_availability (
    id SERIAL PRIMARY KEY,
    room_type_id integer,
    date date,
    available_quantity integer NOT NULL DEFAULT 0,
    CONSTRAINT fk_room_availability_room_type FOREIGN KEY (room_type_id) REFERENCES room_types (id),
    UNIQUE (room_type_id, date)
);

CREATE TABLE chat_rooms (
    id SERIAL PRIMARY KEY,
    hotel_id integer NOT NULL,
    last_message text,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp DEFAULT CURRENT_TIMESTAMP,
    user1_id integer NOT NULL,
    user2_id integer NOT NULL,
    CONSTRAINT fk_chatrooms_hotel FOREIGN KEY (hotel_id) REFERENCES hotels (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_chatrooms_user1 FOREIGN KEY (user1_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_chatrooms_user2 FOREIGN KEY (user2_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE INDEX idx_chatrooms_hotel_id ON chat_rooms (hotel_id);
CREATE INDEX idx_chatrooms_user1_id ON chat_rooms (user1_id);
CREATE INDEX idx_chatrooms_user2_id ON chat_rooms (user2_id);

CREATE TABLE favorites (
    id SERIAL PRIMARY KEY,
    user_id integer NOT NULL,
    hotel_id integer NOT NULL,
    CONSTRAINT fk_favorites_hotel FOREIGN KEY (hotel_id) REFERENCES hotels (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_favorites_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE,
    UNIQUE (user_id, hotel_id)
);
CREATE INDEX idx_favorites_hotel_id ON favorites (hotel_id);

CREATE TABLE histories (
    id SERIAL PRIMARY KEY,
    user_id integer NOT NULL,
    city_id integer,
    district_id integer,
    check_in_date date NOT NULL,
    check_out_date date NOT NULL,
    adults integer NOT NULL,
    kids integer NOT NULL DEFAULT 0,
    search_time timestamp NOT NULL
);
CREATE INDEX idx_histories_user_id ON histories (user_id);
CREATE INDEX idx_histories_city_id ON histories (city_id);
CREATE INDEX idx_histories_district_id ON histories (district_id);

CREATE TABLE orders (
    id SERIAL PRIMARY KEY,
    user_id integer NOT NULL,
    check_in_date date NOT NULL,
    check_out_date date NOT NULL,
    status varchar(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING','CONFIRMED','CANCELED')),
    total_price decimal(10,0),
    created_at timestamp NOT NULL,
    CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE INDEX idx_orders_user_id ON orders (user_id);

CREATE TABLE order_items (
    id SERIAL PRIMARY KEY,
    order_id integer NOT NULL,
    room_type_id integer NOT NULL,
    quantity integer NOT NULL,
    price_per_room decimal(10,0) NOT NULL,
    subtotal decimal(10,0) NOT NULL,
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_order_items_room_type FOREIGN KEY (room_type_id) REFERENCES room_types (id) ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE INDEX idx_order_items_order_id ON order_items (order_id);
CREATE INDEX idx_order_items_room_type_id ON order_items (room_type_id);

CREATE TABLE payments (
    id SERIAL PRIMARY KEY,
    order_id integer NOT NULL,
    method varchar(20) NOT NULL DEFAULT 'CASH' CHECK (method IN ('CREDIT_CARD','CASH','ECPAY')),
    status varchar(20) NOT NULL DEFAULT 'UNPAID' CHECK (status IN ('PAID','UNPAID','CANCELLED','CANCELED')),
    created_at timestamp NOT NULL,
    CONSTRAINT fk_payments_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE INDEX idx_payments_order_id ON payments (order_id);

CREATE TABLE reviews (
    order_id integer PRIMARY KEY,
    user_id integer NOT NULL,
    hotel_id integer NOT NULL,
    score integer NOT NULL,
    comment text,
    reply text,
    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reviews_hotel FOREIGN KEY (hotel_id) REFERENCES hotels (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_reviews_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_reviews_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT chk_reviews_score CHECK (score BETWEEN 1 AND 10)
);
CREATE INDEX idx_reviews_user_id ON reviews (user_id);
CREATE INDEX idx_reviews_hotel_id ON reviews (hotel_id);

CREATE TABLE messages (
    id SERIAL PRIMARY KEY,
    sender_id integer NOT NULL,
    receiver_id integer NOT NULL,
    hotel_id integer NOT NULL,
    content text NOT NULL,
    sent_at timestamp DEFAULT CURRENT_TIMESTAMP,
    is_read boolean NOT NULL DEFAULT false,
    chatroom_id integer,
    CONSTRAINT fk_messages_chatroom FOREIGN KEY (chatroom_id) REFERENCES chat_rooms (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_messages_hotel FOREIGN KEY (hotel_id) REFERENCES hotels (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_messages_sender FOREIGN KEY (sender_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_messages_receiver FOREIGN KEY (receiver_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE INDEX idx_messages_sender_id ON messages (sender_id);
CREATE INDEX idx_messages_receiver_id ON messages (receiver_id);
CREATE INDEX idx_messages_hotel_id ON messages (hotel_id);
CREATE INDEX idx_messages_chatroom_id ON messages (chatroom_id);

CREATE TABLE images (
    id SERIAL PRIMARY KEY,
    hotel_id integer NOT NULL,
    img_url text NOT NULL,
    is_cover boolean NOT NULL,
    CONSTRAINT fk_images_hotel FOREIGN KEY (hotel_id) REFERENCES hotels (id) ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE INDEX idx_images_hotel_id ON images (hotel_id);
