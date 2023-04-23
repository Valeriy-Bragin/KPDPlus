package com.meriniguan.kpdplus.data.room

import androidx.room.*
import com.meriniguan.kpdplus.data.preferences.ShowSetting
import com.meriniguan.kpdplus.data.preferences.SortOrder
import kotlinx.coroutines.flow.Flow

@Dao
interface ToolDao {

    fun getTools(
        searchInput: String,
        sortOrder: SortOrder,
        showSetting: ShowSetting
    ): Flow<List<Tool>> =
        when (sortOrder) {
            SortOrder.BY_DATE_CREATED -> {
                when (showSetting) {
                    ShowSetting.ALL -> getAllToolsSortedByDateCreated(searchInput)
                    ShowSetting.FREE -> getFreeToolsSortedByDateCreated(searchInput)
                    ShowSetting.HELD -> getHeldToolsSortedByDateCreated(searchInput)
                }
            }
            SortOrder.BY_NAME -> {
                when (showSetting) {
                    ShowSetting.ALL -> getAllToolsSortedByName(searchInput)
                    ShowSetting.FREE -> getFreeToolsSortedByName(searchInput)
                    ShowSetting.HELD -> getHeldToolsSortedByName(searchInput)
                }
            }
        }

    @Query("SELECT * FROM tools WHERE name || holder_name LIKE '%' || :searchInput || '%' ORDER BY date_created")
    fun getAllToolsSortedByDateCreated(searchInput: String): Flow<List<Tool>>

    @Query("SELECT * FROM tools WHERE (holder_name == '') AND name LIKE '%' || :searchInput || '%' ORDER BY date_created")
    fun getFreeToolsSortedByDateCreated(searchInput: String): Flow<List<Tool>>

    @Query("SELECT * FROM tools WHERE (holder_name != '') AND name LIKE '%' || :searchInput || '%' ORDER BY date_created")
    fun getHeldToolsSortedByDateCreated(searchInput: String): Flow<List<Tool>>

    @Query("SELECT * FROM tools WHERE name LIKE '%' || :searchInput || '%' ORDER BY name")
    fun getAllToolsSortedByName(searchInput: String): Flow<List<Tool>>

    @Query("SELECT * FROM tools WHERE (holder_name = '') AND name LIKE '%' || :searchInput || '%' ORDER BY name")
    fun getFreeToolsSortedByName(searchInput: String): Flow<List<Tool>>

    @Query("SELECT * FROM tools WHERE (holder_name != '') AND name LIKE '%' || :searchInput || '%' ORDER BY name")
    fun getHeldToolsSortedByName(searchInput: String): Flow<List<Tool>>

    @Query("SELECT * FROM tools WHERE code == :code")
    fun getToolByCode(code: String): Flow<Tool>

    @Query("SELECT COUNT(id) FROM tools")
    fun getRowCount(): Flow<Int>

    @Query("SELECT id FROM tools WHERE code == :code")
    suspend fun findToolByCode(code: String): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tool: Tool): Long

    @Update
    suspend fun update(tool: Tool)

    @Delete
    suspend fun delete(tool: Tool)

}