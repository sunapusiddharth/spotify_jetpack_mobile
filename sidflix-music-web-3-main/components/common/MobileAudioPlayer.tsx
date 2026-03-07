// import React from "react";
// import Hls from "hls.js";
// import Script from 'next/script'

import React from "react";
import { ReactNode } from "react";

// export default class MobileAudioPlayer extends React.Component {
//     state = {};
//     componentDidMount() {
//         const video = this.player;
//         console.log("supported", Hls.isSupported())
//         const hls = new Hls({
//             xhrSetup: function (xhr, url) {
//                 if (url.indexOf('https://dpashj04akcoe.cloudfront.net/') === 0) {
//                     if (url.slice(-2) == 'ts') {
//                         url += '?' + `Policy=${this.props.policy}&Key-Pair-Id=K3QOPS3KJYTLPK&Signature=${this.props.signature}`; // this suffix is updated every [x] seconds by polling the server
//                         xhr.open('GET', url, true);
//                     }
//                 }
//             },
//         });
//         const url = this.props.streamLink;
//         console.log("props", this.props)
//         hls.loadSource(url);
//         hls.attachMedia(video);
//         hls.on(Hls.Events.MANIFEST_PARSED, function () {
//             console.log("play called event", video); video.play();
//         });
//     }
//     render() {
//         return (
//             <>
//                 <Script src="https://cdn.jsdelivr.net/npm/hls.js@latest"/>
//                 <audio
//                     className="videoCanvas"
//                     ref={player => (this.player = player)}
//                     autoPlay={true}
//                     controls={true}
//                     muted={false}

//                 />
//             </>

//         );
//     }
// }

export default class MobileAudioPlayer extends React.Component {
    render(): ReactNode {
        return <></>
    }
}