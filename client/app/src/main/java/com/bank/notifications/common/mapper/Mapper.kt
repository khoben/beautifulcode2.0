package com.bank.notifications.common.mapper

interface Mapper<SRC, DST> {
    fun map(data: SRC): MapperResult<DST>
}

