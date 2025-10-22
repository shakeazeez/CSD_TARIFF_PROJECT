use std::fmt::Display;

use crate::schema::{user_role, user};
use diesel::sql_types::Int4;
use diesel::Selectable;
use diesel::{
    deserialize::{FromSql, FromSqlRow},
    expression::AsExpression,
    pg::Pg,
    prelude::{Identifiable, Insertable, Queryable},
    serialize::ToSql,
};
use serde::{Deserialize, Serialize};



#[derive(Queryable, Selectable, Debug, Identifiable, Serialize, Clone)]
#[diesel(table_name = user)]
pub struct AuthUser {
    pub id: i32,
    pub username: String,
    pub hashed_password: String,
}

#[derive(Queryable, Identifiable, Insertable, Debug, Selectable, Clone)]
#[diesel(table_name = user_role, primary_key(user_id, user_roles))]
pub struct UserRole {
    pub user_id: i32,
    pub user_roles: Role,
}

#[repr(i32)]
#[derive(
    Debug,
    Clone,
    Copy,
    Serialize,
    AsExpression,
    FromSqlRow,
    Deserialize,
    PartialEq,
    Hash,
    Eq,
    PartialOrd,
    Ord,
)]
#[diesel(sql_type = Int4)]
pub enum Role {
    MEMBER,
    BANK,
    BUSINESS,
    ADMIN,
}

impl Role {
    pub fn from_i32(value: i32) -> Self {
        match value {
            0 => Role::MEMBER,
            1 => Role::BANK,
            2 => Role::BUSINESS,
            3 => Role::ADMIN,
            _ => {
                panic!("Whoops, time to update")
            }
        }
    }
    
    pub fn from_string(str: &str) -> Self {
        match str {
            "MEMBER" => Role::MEMBER,
            "BANK" => Role::BANK,
            "BUSINESS" => Role::BUSINESS,
            "ADMIN" => Role::ADMIN,
            _ => {
                panic!("Whoops, time to update")
            }
        }
    }
}

impl Display for Role {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        let s = match self {
            Role::MEMBER => "MEMBER",
            Role::BANK => "BANK",
            Role::BUSINESS => "BUSINESS",
            Role::ADMIN => "ADMIN"
        };
        
        write!(f, "{}", s)
    }
}

impl FromSql<Int4, Pg> for Role {
    fn from_sql(
        bytes: <Pg as diesel::backend::Backend>::RawValue<'_>,
    ) -> diesel::deserialize::Result<Self> {
        let value = i32::from_sql(bytes)?;
        Ok(Role::from_i32(value))
    }
}

impl ToSql<Int4, Pg> for Role {
    fn to_sql<'b>(
        &'b self,
        out: &mut diesel::serialize::Output<'b, '_, Pg>,
    ) -> diesel::serialize::Result {
        match self {
            Role::MEMBER => ToSql::<Int4, Pg>::to_sql(&0i32, out),
            Role::BANK => ToSql::<Int4, Pg>::to_sql(&1i32, out),
            Role::BUSINESS => ToSql::<Int4, Pg>::to_sql(&2i32, out),
            Role::ADMIN => ToSql::<Int4, Pg>::to_sql(&3i32, out),
        }
    }
}
