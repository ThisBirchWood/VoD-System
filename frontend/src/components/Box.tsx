import clsx from "clsx";

type Props = {
    children: React.ReactNode;
    className?: string;
};

const Box = ({ children, className }: Props) => (
    <div className={clsx("bg-white border border-gray-200 shadow-sm rounded-lg", className)}>
        {children}
    </div>
);

export default Box;
