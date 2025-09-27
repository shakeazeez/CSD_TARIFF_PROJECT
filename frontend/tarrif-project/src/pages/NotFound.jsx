import notFound from '../assets/404.png'

export function NotFound(){

    return(
        <>
        <div className='flex items-center justify-center'>
            <img src={notFound} width={500} height={500}/>
        </div>
        </>
    );

}