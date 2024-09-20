package com.github.enteraname74.info901project.domain.model.ext

fun Any.isInteger(): Boolean =
    this.toString().toIntOrNull() != null

fun Any.toInt(): Int? =
    this.toString().toIntOrNull()