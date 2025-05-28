import React from "react";

type Props = React.ButtonHTMLAttributes<HTMLButtonElement>;

const MenuButton: React.FC<Props> = ({ className = "", ...props }) => {
    return (
        <button
            className={`text-gray-800 text-base px-3 py-2 rounded-lg hover:bg-gray-100 hover:text-black transition-colors duration-200 ${className}`}
            {...props}
        />
    );
};

export default MenuButton;