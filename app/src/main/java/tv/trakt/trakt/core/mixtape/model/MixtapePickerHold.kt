package tv.trakt.trakt.core.mixtape.model

internal class MixtapePickerHold {
    private var settled: MixtapePicker? = null

    fun resolve(
        picker: MixtapePicker,
        settling: Boolean,
        historyLoading: Boolean,
    ): MixtapePicker {
        if (historyLoading) return picker
        if (!settling) {
            settled = picker
            return picker
        }
        return settled ?: picker
    }

    fun reset() {
        settled = null
    }
}
