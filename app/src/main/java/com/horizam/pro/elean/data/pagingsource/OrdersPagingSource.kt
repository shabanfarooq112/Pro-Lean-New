package com.horizam.pro.elean.data.pagingsource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.horizam.pro.elean.Constants
import com.horizam.pro.elean.data.api.ApiHelper
import com.horizam.pro.elean.data.model.response.Order
import com.horizam.pro.elean.data.model.response.Subcategory
import retrofit2.HttpException
import java.io.IOException

class OrdersPagingSource(
    private val apiHelper: ApiHelper,
    private val id:String
): PagingSource<Int, Order>() {

        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Order> {

            val position = params.key ?: Constants.STARTING_PAGE_INDEX
            return try {
                val response = apiHelper.
                getBuyerOrders(id,position)
                val buyerOrder = response.orderList
              LoadResult.Page(
                    data = buyerOrder,
                    prevKey = if (position == Constants.STARTING_PAGE_INDEX)
                    {
                        null
                    }
                    else {
                        position - 1
                    },
                    nextKey = if (buyerOrder.isEmpty()) null else position + 1
                )
            }catch (exception: IOException){
              LoadResult.Error(exception)
            }catch (exception: HttpException){
               LoadResult.Error(exception)
            }
        }

        override fun getRefreshKey(state: PagingState<Int, Order>): Int? {
            return state.anchorPosition?.let { anchorPosition ->
                state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                    ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
            }
        }
}