package sigma.software.leovegas.drugstore.extensions

import org.springframework.core.ParameterizedTypeReference

inline fun <reified T> respTypeRef() = object : ParameterizedTypeReference<T>() {}
