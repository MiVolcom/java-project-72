
@import hexlet.code.dto.UrlPage
@param UrlPage page

@template.jte.layout.page(
page = page,
content = @`
    <h1>${page.getUrl().getName()}</h1>
`
)