import kotlinx.browser.document

fun main()
{
    val result = test()

    document.getElementById("resultDiv")?.textContent = result
}
