package abandonedstudio.app.compassproject.model

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class DestinationRepositoryTest {

    private lateinit var repository: DestinationRepository
    private lateinit var destination: Destination

    @Before
    fun setup(){
        destination = Destination()
        repository = DestinationRepository(destination)
    }

    @Test
    fun testSetDestinationCoordinatesWorkingProperly(){
        repository.setDestinationCoordinates(51.1, 16.6)
        Truth.assertThat(destination).isEqualTo(Destination(51.1, 16.6))
    }

    @Test
    fun testGetDestinationLatitudeAfterMultipleChanges(){
        val listOfLatitudes = mutableListOf<Double>()
        repository.setDestinationCoordinates(51.2, 16.6)
        listOfLatitudes.add(repository.getDestinationLatitude())
        repository.setDestinationCoordinates(22.7, 16.6)
        listOfLatitudes.add(repository.getDestinationLatitude())
        Truth.assertThat(listOfLatitudes).isEqualTo(mutableListOf(51.2, 22.7))
    }

}