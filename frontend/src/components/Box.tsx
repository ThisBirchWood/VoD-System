import clsx from "clsx";

type props = {
    children: React.ReactNode,
    className?: string
}

const Box = ({children, className}: props) => {
    return (
        <div className={clsx("bg-gray-200 shadow-lg rounded-lg", className)}>
            { children }
        </div>
    )
}

export default Box;