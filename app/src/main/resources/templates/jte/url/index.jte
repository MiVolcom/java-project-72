@import hexlet.code.dto.UrlsPage
@param UrlsPage page

@template.jte.layout.page(content =
@`
    <h1>Sites list</h1>

    <a href="${"/"}">Add site</a>
    @if(page.getUrls().isEmpty())
        <p>Site list is empty</p>
    @else
        @for(var url : page.getUrls())
            <div>
                <h2>
                    <a href= "${"/urls/" + url.getId()}"> ${url.getName()}</a>
                </h2>
            </div>
        @endfor
    @endif
`
)