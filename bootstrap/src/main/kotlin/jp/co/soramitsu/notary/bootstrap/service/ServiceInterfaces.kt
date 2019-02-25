package jp.co.soramitsu.notary.bootstrap.service

import jp.co.soramitsu.notary.bootstrap.dto.GenesisData

interface GenesisBlockService {
    fun getGenericData(): GenesisData
}