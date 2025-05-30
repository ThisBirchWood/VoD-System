import React from "react";
import clsx from "clsx";

type Props = React.ButtonHTMLAttributes<HTMLButtonElement>;

const MenuButton: React.FC<Props> = ({ className = "", ...props }) => {
    return (
        <button
            className={clsx("p-2 rounded-lg hover:bg-gray-300 hover:text-black transition-colors duration-200", className)}
            {...props}
        />
    );
};

export default MenuButton;