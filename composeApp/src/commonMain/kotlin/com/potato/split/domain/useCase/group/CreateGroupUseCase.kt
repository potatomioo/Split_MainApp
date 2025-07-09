package com.potato.split.domain.useCase.group

import com.potato.split.contact.Contact
import com.potato.split.data.Repository.GroupRepository
import com.potato.split.data.network.models_app.Group

class CreateGroupUseCase(
    private val groupRepository: GroupRepository
) {
    suspend operator fun invoke(name: String, members: List<Contact>): Result<Group> =
        groupRepository.createGroup(name, members)
}