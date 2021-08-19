package sigma.software.leovegas.drugstore.product

import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component

@Component
class SortUtil {
    companion object {
        fun getSort(sortField: String?, sortDirection: String): Sort {
            val sort: Sort =
                if (sortField == "default") {
                    Sort.unsorted()
                } else {
                    if ("ASC" == sortDirection) {
                        Sort.by(sortField).ascending()
                    } else {
                        Sort.by(sortField).descending()
                    }
                }
            return sort
        }
    }
}