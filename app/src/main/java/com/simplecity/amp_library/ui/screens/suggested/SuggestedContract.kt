package com.simplecity.amp_library.ui.screens.suggested

import com.simplecity.amp_library.ui.screens.album.menu.AlbumMenuContract
import com.simplecity.amp_library.ui.screens.songs.menu.SongMenuContract
import com.simplecity.amp_library.ui.screens.suggested.SuggestedPresenter.SuggestedData

fun interface SuggestedContract {

    fun interface Presenter {

        fun loadData()

    }

    fun interface View : AlbumMenuContract.View, SongMenuContract.View {

        fun setData(suggestedData: SuggestedData)
    }

}