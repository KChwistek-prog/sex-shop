package com.pleasure
package shared

enum AppError:
  case NotFound(entity: String, id: String)
  case ValidationError(message: String)
  case InputError(message: String)

  def msg: String = this match
    case NotFound(entity, id) => s"$entity not found $id"
    case ValidationError(msg) => s"Validation failed: $msg"
    case InputError(msg) => s"Invalid input: $msg"
  