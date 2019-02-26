package jp.co.soramitsu.notary.bootstrap.genesis

import iroha.protocol.Primitive
import jp.co.soramitsu.iroha.java.Transaction
import jp.co.soramitsu.iroha.testcontainers.detail.GenesisBlockBuilder


fun createNotaryRole(builder: GenesisBlockBuilder) {
    builder.addTransaction(
        Transaction.builder(null)
            // create default "user" role
            .createRole(
                "notary",
                listOf(
                    Primitive.RolePermission.can_get_all_acc_ast,
                    Primitive.RolePermission.can_get_all_accounts,
                    Primitive.RolePermission.can_create_asset,
                    Primitive.RolePermission.can_add_asset_qty,
                    Primitive.RolePermission.can_transfer,
                    Primitive.RolePermission.can_set_detail,
                    Primitive.RolePermission.can_get_all_txs,
                    Primitive.RolePermission.can_receive,
                    Primitive.RolePermission.can_get_blocks,
                    Primitive.RolePermission.can_read_assets,
                    Primitive.RolePermission.can_add_signatory,
                    Primitive.RolePermission.can_set_quorum,
                    Primitive.RolePermission.can_grant_can_set_my_quorum,
                    Primitive.RolePermission.can_grant_can_add_my_signatory,
                    Primitive.RolePermission.can_grant_can_transfer_my_assets
                )
            ).build()
            .build()
    )
}

fun createRelayDeployerRole(builder:GenesisBlockBuilder) {
    builder.addTransaction(
        Transaction.builder(null)
            // create default "user" role
            .createRole(
                "relay_deployer",
                listOf(
                    Primitive.RolePermission.can_set_detail,
                    Primitive.RolePermission.can_get_domain_accounts
                )
            ).build()
            .build()
    )
}

fun createEthTokenListStorageRole(builder:GenesisBlockBuilder) {
    builder.addTransaction(
        Transaction.builder(null)
            // create default "user" role
            .createRole(
                "eth_token_list_storage",
                listOf(
                    Primitive.RolePermission.can_set_detail,
                    Primitive.RolePermission.can_create_asset
                )
            ).build()
            .build()
    )
}

fun createRegistrationServiceRole(builder:GenesisBlockBuilder) {
    builder.addTransaction(
        Transaction.builder(null)
            // create default "user" role
            .createRole(
                "registration_service",
                listOf(
                    Primitive.RolePermission.can_create_account,
                    Primitive.RolePermission.can_append_role,
                    Primitive.RolePermission.can_set_detail,
                    Primitive.RolePermission.can_get_all_accounts,
                    Primitive.RolePermission.can_get_domain_accounts,
                    Primitive.RolePermission.can_get_all_txs,
                    Primitive.RolePermission.can_get_blocks,
                    Primitive.RolePermission.can_set_quorum
                )
            ).build()
            .build()
    )
}

fun createClientRole(builder:GenesisBlockBuilder) {
    builder.addTransaction(
        Transaction.builder(null)
            // create default "user" role
            .createRole(
                "client",
                listOf(
                    Primitive.RolePermission.can_get_my_account,
                    Primitive.RolePermission.can_get_my_acc_ast,
                    Primitive.RolePermission.can_get_my_acc_ast_txs,
                    Primitive.RolePermission.can_get_my_acc_txs,
                    Primitive.RolePermission.can_get_my_txs,
                    Primitive.RolePermission.can_transfer,
                    Primitive.RolePermission.can_receive,
                    Primitive.RolePermission.can_set_quorum,
                    Primitive.RolePermission.can_add_signatory,
                    Primitive.RolePermission.can_get_my_signatories,
                    Primitive.RolePermission.can_remove_signatory
                )
            ).build()
            .build()
    )
}

fun createWithdrawalRole(builder:GenesisBlockBuilder) {
    builder.addTransaction(
        Transaction.builder(null)
            // create default "user" role
            .createRole(
                "withdrawal",
                listOf(
                    Primitive.RolePermission.can_get_all_accounts,
                    Primitive.RolePermission.can_get_blocks,
                    Primitive.RolePermission.can_read_assets,
                    Primitive.RolePermission.can_receive,
                    Primitive.RolePermission.can_get_all_txs
                )
            ).build()
            .build()
    )
}

fun createSignatureCollectorRole(builder:GenesisBlockBuilder) {
    builder.addTransaction(
        Transaction.builder(null)
            // create default "user" role
            .createRole(
                "signature_collector",
                listOf(
                    Primitive.RolePermission.can_create_account,
                    Primitive.RolePermission.can_set_detail,
                    Primitive.RolePermission.can_get_all_accounts
                )
            ).build()
            .build()
    )
}

fun createVacuumerRole(builder:GenesisBlockBuilder) {
    builder.addTransaction(
        Transaction.builder(null)
            // create default "user" role
            .createRole(
                "Vacuumer",
                listOf(
                    Primitive.RolePermission.can_get_domain_accounts,
                    Primitive.RolePermission.can_read_assets
                )
            ).build()
            .build()
    )
}

fun createNoneRole(builder:GenesisBlockBuilder) {
    builder.addTransaction(
        Transaction.builder(null)
            // create default "user" role
            .createRole(
                "none",
                listOf()
            ).build()
            .build()
    )
}

fun createTesterRole(builder:GenesisBlockBuilder) {
    builder.addTransaction(
        Transaction.builder(null)
            // create default "user" role
            .createRole(
                "tester",
                listOf(
                    Primitive.RolePermission.can_create_account,
                    Primitive.RolePermission.can_set_detail,
                    Primitive.RolePermission.can_create_asset,
                    Primitive.RolePermission.can_transfer,
                    Primitive.RolePermission.can_receive,
                    Primitive.RolePermission.can_add_asset_qty,
                    Primitive.RolePermission.can_subtract_asset_qty,
                    Primitive.RolePermission.can_create_domain,
                    Primitive.RolePermission.can_grant_can_add_my_signatory,
                    Primitive.RolePermission.can_grant_can_remove_my_signatory,
                    Primitive.RolePermission.can_grant_can_set_my_quorum,
                    Primitive.RolePermission.can_grant_can_transfer_my_assets,
                    Primitive.RolePermission.can_add_peer,
                    Primitive.RolePermission.can_append_role,
                    Primitive.RolePermission.can_create_role,
                    Primitive.RolePermission.can_detach_role,
                    Primitive.RolePermission.can_add_signatory,
                    Primitive.RolePermission.can_remove_signatory,
                    Primitive.RolePermission.can_set_quorum,
                    Primitive.RolePermission.can_get_all_acc_detail,
                    Primitive.RolePermission.can_get_all_accounts,
                    Primitive.RolePermission.can_get_all_acc_ast,
                    Primitive.RolePermission.can_get_blocks,
                    Primitive.RolePermission.can_get_roles,
                    Primitive.RolePermission.can_get_all_signatories,
                    Primitive.RolePermission.can_get_domain_accounts,
                    Primitive.RolePermission.can_get_all_txs,
                    Primitive.RolePermission.can_get_domain_acc_detail,
                    Primitive.RolePermission.can_read_assets
                )
            ).build()
            .build()
    )
}

fun createWhiteListSetterRole(builder:GenesisBlockBuilder) {
    builder.addTransaction(
        Transaction.builder(null)
            // create default "user" role
            .createRole(
                "whitelist_setter",
                listOf(
                    Primitive.RolePermission.can_set_detail
                )
            ).build()
            .build()
    )
}

fun createRollBackRole(builder:GenesisBlockBuilder) {
    builder.addTransaction(
        Transaction.builder(null)
            // create default "user" role
            .createRole(
                "rollback",
                listOf(
                    Primitive.RolePermission.can_transfer
                )
            ).build()
            .build()
    )
}

fun createNotaryListHolderRole(builder:GenesisBlockBuilder) {
    builder.addTransaction(
        Transaction.builder(null)
            // create default "user" role
            .createRole(
                "notary_list_holder",
                listOf(
                    Primitive.RolePermission.can_set_detail
                )
            ).build()
            .build()
    )
}

fun createSoraRole(builder:GenesisBlockBuilder) {
    builder.addTransaction(
        Transaction.builder(null)
            // create default "user" role
            .createRole(
                "notary_list_holder",
                listOf(
                    Primitive.RolePermission.can_get_my_acc_ast,
                    Primitive.RolePermission.can_transfer,
                    Primitive.RolePermission.can_receive
                )
            ).build()
            .build()
    )
}

fun createSoraClientRole(builder:GenesisBlockBuilder) {
    builder.addTransaction(
        Transaction.builder(null)
            // create default "user" role
            .createRole(
                "notary_list_holder",
                listOf(
                    Primitive.RolePermission.can_get_my_account,
                    Primitive.RolePermission.can_get_my_acc_ast,
                    Primitive.RolePermission.can_get_my_acc_ast_txs,
                    Primitive.RolePermission.can_get_my_acc_txs,
                    Primitive.RolePermission.can_get_my_txs,
                    Primitive.RolePermission.can_transfer,
                    Primitive.RolePermission.can_receive,
                    Primitive.RolePermission.can_set_quorum,
                    Primitive.RolePermission.can_add_signatory,
                    Primitive.RolePermission.can_get_my_signatories,
                    Primitive.RolePermission.can_remove_signatory
                )
            ).build()
            .build()
    )
}

fun createBtcFeeRateSetterRole(builder:GenesisBlockBuilder) {
    builder.addTransaction(
        Transaction.builder(null)
            // create default "user" role
            .createRole(
                "notary_list_holder",
                listOf(
                    Primitive.RolePermission.can_set_detail,
                    Primitive.RolePermission.can_get_all_accounts
                )
            ).build()
            .build()
    )
}
