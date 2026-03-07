
let  stations = []
$('.col-md-five').each(function(){
        let title = $(this).find('.station-in-list-title').text()
        let img = $(this).find('.station-in-list-logo').attr('src')
        let desc = $(this).find('.station-in-list-desc').text()
        let cats = $(this).find('.cat-in-list').text()
        let url_hi = $(this).find('.play-pause-button').attr('data-hi')
        let url_med = $(this).find('.play-pause-button').attr('data-med')
        let url_low = $(this).find('.play-pause-button').attr('data-low')
        stations.push({
            'favicon': 'https://pcradio.ru'+img,
            'name': title,
            'tags': cats,
            'url': [url_hi,url_low,url_med].join(','),
            desc
        })
})
console.log(stations[0])

