package com.ap.watchtogive.data.repository

import com.ap.watchtogive.model.Charity
import kotlinx.coroutines.flow.Flow

interface CharitiesRepository { //Todo: implement stateflow here
    fun getCharitiesByLocation(location: String): Flow<List<Charity>>
}
