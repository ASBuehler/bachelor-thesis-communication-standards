namespace java sqtest.thrift

struct Person {
  1: required string name,
  2: required i32 age,
  3: optional string email
}