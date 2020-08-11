package com.biblefoundry.ibcsundayopenseats.cli

import com.github.ajalt.clikt.core.subcommands

fun main(args: Array<String>) = AppCommand().subcommands(ListCommand(), UpsertCommand(), DeleteCommand()).main(args)