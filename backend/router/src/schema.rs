


diesel::table! {
    user (id) {
        id -> Int4,
        username -> Varchar,
        hashedpassword -> Varchar,
        // user_type -> Varchar,
    }
}

diesel::table! {
    user_role (user_id, user_roles) {
        user_id -> Int4,
        user_roles -> Int2
    }
}

diesel::joinable!(user_role -> user (user_id));
diesel::allow_tables_to_appear_in_same_query!(user, user_role);