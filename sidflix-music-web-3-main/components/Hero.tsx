import { signIn } from "next-auth/react";
import Head from "next/head";
import Image from "next/image";

function Hero() {
  const signInRedirection = ()=>{
    signIn('auth0')
  }
  return (
    <section className="relative">
      <Head>
        <title>Log in | Disney+</title>
        <link rel="icon" href="/favicon.ico" />
      </Head>
      <div className="relative min-h-[calc(100vh-72px)]">
        <Image
          src="/images/hero-background.jpg"
          layout="fill"
          objectFit="cover"
        />
      </div>
      <div className="flex justify-center items-center" style={{
        position: 'fixed',
        left: '50%',
        top: '50%',
        transform: 'translate(-50%, -50%)'
      }}>
        <div className="absolute flex flex-col space-y-3 top-1/4 w-full justify-center items-center max-w-screen-sm mx-auto p-8 -mt-16 landing-text">
          {/* <Image
            src="/images/cta-logo-one.svg"
            width="600"
            height="150"
            objectFit="contain"
          /> */}
          <p>Click below to watch movies and tv shows</p>
          <button className="bg-blue-600 uppercase text-xl tracking-wide font-extrabold py-4 px-6 w-full rounded hover:bg-[#0485ee] landin-button" onClick={signInRedirection}>
            Watch now
          </button>
          {/* <Image
            src="/images/cta-logo-two.png"
            width="600"
            height="70"
            objectFit="contain"
          /> */}
        </div>
      </div>
    </section>
  );
}

export default Hero;
