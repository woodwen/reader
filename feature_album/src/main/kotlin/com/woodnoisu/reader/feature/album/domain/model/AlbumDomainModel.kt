package com.woodnoisu.reader.feature.album.domain.model

internal data class AlbumDomainModel(val currentPage:Int = 1,
                                     val totalPage:Int = 1,
                                     val albumBookDomainModels: List<AlbumBookDomainModel>?=null,
                                     val albumBookDomainModel :AlbumBookDomainModel? =null)