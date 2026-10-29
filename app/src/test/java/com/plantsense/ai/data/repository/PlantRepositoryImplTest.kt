package com.plantsense.ai.data.repository

import com.plantsense.ai.core.network.NetworkResult
import com.plantsense.ai.data.local.PlantLocalDataSource
import com.plantsense.ai.data.remote.GeminiDiseaseDataSource
import com.plantsense.ai.data.remote.GeminiPlantDataSource
import com.plantsense.ai.domain.model.PlantIdentificationResult
import com.plantsense.ai.domain.repository.SettingsRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class PlantRepositoryImplTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val plantDataSource: GeminiPlantDataSource = mockk()
    private val diseaseDataSource: GeminiDiseaseDataSource = mockk()
    private val localDataSource: PlantLocalDataSource = mockk()
    private val settingsRepository: SettingsRepository = mockk()
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var repository: PlantRepositoryImpl

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = PlantRepositoryImpl(
            plantDataSource,
            diseaseDataSource,
            localDataSource,
            settingsRepository,
            testDispatcher
        )
    }

    @Test
    fun identifyPlant_fileDoesNotExist_returnsUnknownError() = runTest {
        val result = repository.identifyPlant("non_existent_file.jpg")
        assertTrue(result is NetworkResult.UnknownError)
        assertEquals("Captured image not found.", (result as NetworkResult.UnknownError).throwable.message)
    }

    @Test
    fun identifyPlant_apiKeyIsBlank_returnsApiError() = runTest {
        val file = tempFolder.newFile("test_image.jpg")
        every { settingsRepository.getApiKey() } returns flowOf("")

        val result = repository.identifyPlant(file.absolutePath)
        assertTrue(result is NetworkResult.ApiError)
        assertEquals(401, (result as NetworkResult.ApiError).code)
        assertEquals("API Key is missing. Please set it in Settings.", result.message)
    }

    @Test
    fun identifyPlant_success_returnsSuccess() = runTest {
        val file = tempFolder.newFile("test_image.jpg")
        val expectedResult = PlantIdentificationResult(
            plantName = "Fern",
            botanicalName = "Polypodiopsida",
            confidence = 0.95,
            description = "A green fern",
            careLight = "Indirect",
            careWater = "Moist",
            careTemp = "Warm"
        )
        every { settingsRepository.getApiKey() } returns flowOf("api_key_123")
        coEvery { plantDataSource.identifyPlant("api_key_123", any()) } returns NetworkResult.Success(expectedResult)

        val result = repository.identifyPlant(file.absolutePath)
        assertTrue(result is NetworkResult.Success)
        assertEquals(expectedResult, (result as NetworkResult.Success).data)
    }
}
