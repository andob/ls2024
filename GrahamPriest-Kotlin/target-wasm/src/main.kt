import kotlinx.browser.document

fun main()
{
    //todo kotlin wasm doesn't work
    val result = test()

    document.getElementById("resultDiv")?.textContent = result
}
