package io.micronaut.docs.inject.intro

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import io.micronaut.context.BeanContext

class VehicleSpec : StringSpec({
    "test start vehicle" {
        // tag::start[]
        val vehicle = BeanContext.run().getBean(Vehicle::class.java)
        println(vehicle.start())
        // end::start[]

        vehicle.start().shouldBe("Starting V8")
    }

})
