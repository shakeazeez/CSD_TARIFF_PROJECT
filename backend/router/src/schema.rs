


diesel::table! {
    user (id) {
        id -> Int4,
        username -> Varchar,
        hashedpassword -> Varchar,
        user_roles -> Int2
        // user_type -> Varchar,
    }
}
