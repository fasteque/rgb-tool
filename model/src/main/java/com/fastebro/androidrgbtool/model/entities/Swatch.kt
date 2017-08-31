package com.fastebro.androidrgbtool.model.entities

open class Swatch {
    enum class SwatchType {
        NONE, VIBRANT, LIGHT_VIBRANT, DARK_VIBRANT,
        MUTED, LIGHT_MUTED, DARK_MUTED
    }

    var rgb: Int = 0
    var type: SwatchType

    constructor() {
        this.rgb = 0
        this.type = SwatchType.NONE
    }

    constructor(rgb: Int, type: SwatchType) {
        this.rgb = rgb
        this.type = type
    }
}
